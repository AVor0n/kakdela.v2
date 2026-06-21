import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core';
import type { MouseEventHandler, Ref } from 'react';
import './DragHandle.css';

type DragHandleProps = {
    attributes?: DraggableAttributes;
    listeners?: DraggableSyntheticListeners;
    setNodeRef?: Ref<HTMLDivElement>;
};

export function DragHandle({ attributes, listeners, setNodeRef }: DragHandleProps) {
    const stopQuestionClick: MouseEventHandler<HTMLDivElement> = (event) => {
        event.stopPropagation();
    };

    return (
        <div
            ref={setNodeRef}
            className='drag-handle'
            onClick={stopQuestionClick}
            onMouseDown={stopQuestionClick}
            {...attributes}
            {...listeners}
        >
            <svg className='drag-handle__icon' viewBox='0 0 16 8' fill='none' aria-hidden='true'>
                <circle cx='4' cy='2' r='1.5' fill='currentColor' />
                <circle cx='8' cy='2' r='1.5' fill='currentColor' />
                <circle cx='12' cy='2' r='1.5' fill='currentColor' />
                <circle cx='4' cy='6' r='1.5' fill='currentColor' />
                <circle cx='8' cy='6' r='1.5' fill='currentColor' />
                <circle cx='12' cy='6' r='1.5' fill='currentColor' />
            </svg>
        </div>
    );
}
