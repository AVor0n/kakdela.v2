import { SurveyDetail } from './components/SurveyDetail/SurveyDetail';
import { useAppSelector } from '@/hooks/useAppSelector';
import { Sidebar } from './components/Sidebar/Sidebar';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { reorderPages, setSurveyPages } from '@/entities/Pages/Pages.slice';
import { clonePage } from '@/entities/Survey/Survey.utils';
import { updateSurveyPage } from '@/api/surveyPages';
import { getSurveyForEditById } from '@/api/survey';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import {
    closestCenter,
    DndContext,
    KeyboardSensor,
    PointerSensor,
    useSensor,
    useSensors,
    type DragEndEvent,
} from '@dnd-kit/core';
import { SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { useMemo } from 'react';
import style from './SurveyModify.module.css';
import { SortablePage } from './components/SortablePage/SortablePage';
import { ClosingPageEditor } from './components/ClosingPageEditor/ClosingPageEditor';
import { useSearchParams } from 'react-router-dom';
import type { Page } from '@/shared/types/Survey.type';
import { validateActiveSurveyConditions } from '@/shared/utils/conditions';

function getReorderedPages(pages: Page[], activePageId: string, overPageId: string): Page[] {
    const reorderedPages = pages.map(clonePage);
    const activePageIndex = reorderedPages.findIndex(({ id }) => id === activePageId);
    const overPageIndex = reorderedPages.findIndex(({ id }) => id === overPageId);
    if (activePageIndex < 0 || overPageIndex < 0) return reorderedPages;

    const [activePage] = reorderedPages.splice(activePageIndex, 1);
    reorderedPages.splice(overPageIndex, 0, activePage);
    reorderedPages.forEach((page, index) => {
        page.serialNumber = index + 1;
    });
    return reorderedPages;
}

function getBackwardConditionIds(pages: Page[]): Set<string> {
    return new Set(
        validateActiveSurveyConditions(pages)
            .filter(({ code }) => code === 'TARGET_PAGE_NOT_FORWARD')
            .map(({ conditionId }) => conditionId),
    );
}

export function SurveyModify() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const { pages } = useAppSelector((state) => state.pages);
    const { selectedTemplate } = useAppSelector((state) => state.template);
    const [searchParams] = useSearchParams();
    const isTemplate = searchParams.get('template') === 'true';
    const dispatch = useAppDispatch();
    const pageIds = useMemo(() => pages.map((page) => page.id), [pages]);
    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        }),
    );

    const handlePageDragEnd = (event: DragEndEvent) => {
        const { active, over } = event;
        if (!over || active.id === over.id) return;

        const activePageId = String(active.id);
        const overPageId = String(over.id);
        const overPage = pages.find((page) => page.id === overPageId);
        if (!overPage) return;

        const previousPages = pages.map(clonePage);
        const reorderedPages = getReorderedPages(previousPages, activePageId, overPageId);
        const currentBackwardConditionIds = getBackwardConditionIds(previousPages);
        const newBackwardTransition = validateActiveSurveyConditions(reorderedPages).find(
            ({ code, conditionId }) =>
                code === 'TARGET_PAGE_NOT_FORWARD' && !currentBackwardConditionIds.has(conditionId),
        );
        if (newBackwardTransition) {
            const targetPage = reorderedPages.find(({ id }) => id === newBackwardTransition.targetPageId);
            dispatch(
                setErrorMessage({
                    message: `Нельзя изменить порядок: переход со страницы ${newBackwardTransition.pageSerialNumber} на страницу ${targetPage?.serialNumber ?? '?'} станет обратным`,
                }),
            );
            return;
        }

        dispatch(reorderPages({ activePageId, overPageId }));

        updateSurveyPage(activePageId, { serialNumber: overPage.serialNumber })
            .then(() =>
                getSurveyForEditById(selectedSurvey.id)
                    .then((survey) => dispatch(setSurveyPages({ pages: survey.pages })))
                    .catch(() =>
                        dispatch(
                            setErrorMessage({ message: 'Порядок изменён, но не удалось обновить условия перехода' }),
                        ),
                    ),
            )
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось изменить порядок страниц' }));
                dispatch(setSurveyPages({ pages: previousPages }));
            });
    };

    return (
        <div className={style.container}>
            <div className={style.content}>
                <SurveyDetail item={isTemplate ? selectedTemplate! : selectedSurvey!} />
                <DndContext
                    sensors={sensors}
                    collisionDetection={closestCenter}
                    autoScroll
                    onDragEnd={handlePageDragEnd}
                >
                    <SortableContext items={pageIds} strategy={verticalListSortingStrategy}>
                        {pages.map((page, index) => (
                            <SortablePage key={page.id} page={page} pageIndex={index} />
                        ))}
                    </SortableContext>
                </DndContext>
                <ClosingPageEditor
                    surveyId={isTemplate ? selectedTemplate!.id : selectedSurvey!.id}
                    closingPage={isTemplate ? selectedTemplate!.closingPage : selectedSurvey!.closingPage}
                />
            </div>
            <Sidebar />
        </div>
    );
}
