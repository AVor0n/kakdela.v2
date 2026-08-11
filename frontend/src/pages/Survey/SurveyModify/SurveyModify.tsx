import { SurveyDetail } from './components/SurveyDetail/SurveyDetail';
import { useAppSelector } from '@/hooks/useAppSelector';
import { Sidebar } from './components/Sidebar/Sidebar';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { reorderPages, setSurveyPages } from '@/entities/Survey/Survey.slice';
import { clonePage } from '@/entities/Survey/Survey.utils';
import { updateSurveyPage } from '@/api/surveyPages';
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

export function SurveyModify() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();
    const pageIds = useMemo(() => selectedSurvey?.pages.map((page) => page.id) ?? [], [selectedSurvey]);
    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        }),
    );

    const handlePageDragEnd = (event: DragEndEvent) => {
        if (!selectedSurvey) return;

        const { active, over } = event;
        if (!over || active.id === over.id) return;

        const activePageId = String(active.id);
        const overPageId = String(over.id);
        const overPage = selectedSurvey.pages.find((page) => page.id === overPageId);
        if (!overPage) return;

        const previousPages = selectedSurvey.pages.map(clonePage);

        dispatch(reorderPages({ activePageId, overPageId }));

        updateSurveyPage(activePageId, { serialNumber: overPage.serialNumber }).catch(() => {
            dispatch(setErrorMessage({ message: 'Не удалось изменить порядок страниц' }));
            dispatch(setSurveyPages({ pages: previousPages }));
        });
    };

    if (!selectedSurvey) {
        return <div>Загрузка...</div>;
    }
    return (
        <div className={style.container}>
            <div className={style.content}>
                <SurveyDetail survey={selectedSurvey} />
                <DndContext
                    sensors={sensors}
                    collisionDetection={closestCenter}
                    autoScroll
                    onDragEnd={handlePageDragEnd}
                >
                    <SortableContext items={pageIds} strategy={verticalListSortingStrategy}>
                        {selectedSurvey.pages.map((page, index) => (
                            <SortablePage key={page.id} page={page} pageIndex={index} />
                        ))}
                    </SortableContext>
                </DndContext>
                <ClosingPageEditor surveyId={selectedSurvey.id} closingPage={selectedSurvey.closingPage} />
            </div>
            <Sidebar />
        </div>
    );
}
