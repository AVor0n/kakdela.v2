import type { Question } from '@/shared/types/Question.type';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Question as QuestionComponent } from '../Question/Question';

type SortableQuestionProps = {
    question: Question;
    isEditMode: boolean;
    onClick: () => void;
};

export function SortableQuestion({ question, isEditMode, onClick }: SortableQuestionProps) {
    const { attributes, listeners, setActivatorNodeRef, setNodeRef, transform, transition, isDragging } = useSortable({
        id: question.id,
    });

    return (
        <div
            ref={setNodeRef}
            style={{
                transform: CSS.Transform.toString(transform),
                transition,
            }}
        >
            <QuestionComponent
                question={question}
                onClick={onClick}
                isEditMode={isEditMode}
                isDragging={isDragging}
                dragHandleAttributes={attributes}
                dragHandleListeners={listeners}
                dragHandleRef={setActivatorNodeRef}
            />
        </div>
    );
}
