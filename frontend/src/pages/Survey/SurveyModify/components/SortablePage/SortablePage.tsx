import type { Page } from '@/shared/types/Survey.type';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { PageSeparator } from '../PageSeparator/PageSeparator';
import { QuestionList } from '../QuestionList/QuestionList';
import { PageConditionsEditor } from '../PageConditionsEditor/PageConditionsEditor';

type SortablePageProps = {
    page: Page;
    pageIndex: number;
};

export function SortablePage({ page, pageIndex }: SortablePageProps) {
    const { attributes, listeners, setActivatorNodeRef, setNodeRef, transform, transition, isDragging } = useSortable({
        id: page.id,
    });

    return (
        <div
            ref={setNodeRef}
            style={{
                transform: CSS.Transform.toString(transform),
                transition,
                opacity: isDragging ? 0.6 : 1,
            }}
        >
            <PageSeparator
                page={page}
                dragHandleAttributes={attributes}
                dragHandleListeners={listeners}
                dragHandleRef={setActivatorNodeRef}
            />
            <PageConditionsEditor page={page} />
            <QuestionList questions={page.questions} pageNumber={page.serialNumber} pageIndex={pageIndex} />
        </div>
    );
}
