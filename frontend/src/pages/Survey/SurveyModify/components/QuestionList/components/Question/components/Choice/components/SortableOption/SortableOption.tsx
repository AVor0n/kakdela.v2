import type { AnswerOption } from '@/shared/types/Question.type';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { ReactNode } from 'react';
import { Option } from '../Option/Option';

type SortableOptionProps = {
    option: AnswerOption;
    children: ReactNode;
    isEdit: boolean;
};

export function SortableOption({ option, children, isEdit }: SortableOptionProps) {
    const { attributes, listeners, setActivatorNodeRef, setNodeRef, transform, transition, isDragging } = useSortable({
        id: option.id,
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
            <Option
                option={option}
                isEdit={isEdit}
                dragHandleAttributes={attributes}
                dragHandleListeners={listeners}
                dragHandleRef={setActivatorNodeRef}
            >
                {children}
            </Option>
        </div>
    );
}
