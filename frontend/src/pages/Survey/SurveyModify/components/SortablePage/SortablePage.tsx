import type { Page } from '@/shared/types/Survey.type';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { useState } from 'react';
import { PageSeparator } from '../PageSeparator/PageSeparator';
import { QuestionList } from '../QuestionList/QuestionList';
import { PageConditionsEditor } from '../PageConditionsEditor/PageConditionsEditor';
import style from './SortablePage.module.css';

type SortablePageProps = {
    page: Page;
    pageIndex: number;
};

export function SortablePage({ page, pageIndex }: SortablePageProps) {
    const [isConditionsEditorOpen, setIsConditionsEditorOpen] = useState(false);
    const conditionsEditorId = `page-conditions-${page.id}`;
    const { attributes, listeners, setActivatorNodeRef, setNodeRef, transform, transition, isDragging } = useSortable({
        id: page.id,
    });

    return (
        <div
            ref={setNodeRef}
            className={style.page}
            style={{
                transform: CSS.Transform.toString(transform),
                transition,
                opacity: isDragging ? 0.6 : 1,
            }}
        >
            <PageSeparator
                page={page}
                conditionsEditorId={conditionsEditorId}
                isConditionsEditorOpen={isConditionsEditorOpen}
                onToggleConditions={() => setIsConditionsEditorOpen((isOpen) => !isOpen)}
                dragHandleAttributes={attributes}
                dragHandleListeners={listeners}
                dragHandleRef={setActivatorNodeRef}
            />
            {isConditionsEditorOpen && (
                <div id={conditionsEditorId}>
                    <PageConditionsEditor page={page} />
                </div>
            )}
            <QuestionList pageId={page.id} questions={page.questions} pageIndex={pageIndex} />
        </div>
    );
}
