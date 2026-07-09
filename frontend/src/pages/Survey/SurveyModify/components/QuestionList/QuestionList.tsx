import type { Question } from '@/shared/types/Question.type';
import { Question as QuestionComponent } from './components/Question/Question';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { reorderQuestions, setPageQuestions, setSelectedQuestion } from '@/entities/Survey/Survey.slice';
import { cloneQuestion } from '@/entities/Survey/Survey.utils';
import { updateQuestion } from '@/api/question';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import {
    closestCenter,
    DndContext,
    DragOverlay,
    KeyboardSensor,
    PointerSensor,
    useSensor,
    useSensors,
    type DragEndEvent,
    type DragStartEvent,
} from '@dnd-kit/core';
import { SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { useMemo, useState } from 'react';
import { SortableQuestion } from './components/SortableQuestion/SortableQuestion';
import style from './QuestionList.module.css';

interface Props {
    questions: Question[];
    pageNumber: number;
    pageIndex: number;
}

export function QuestionList({ questions, pageIndex }: Props) {
    const { selectedQuestion } = useAppSelector((state) => state.survey);
    const editQuestionId = useMemo(() => selectedQuestion?.id, [selectedQuestion]);
    const dispatch = useAppDispatch();
    const [activeQuestionId, setActiveQuestionId] = useState<string | null>(null);
    const questionIds = useMemo(() => questions.map((question) => question.id), [questions]);
    const activeQuestion = useMemo(() => {
        return questions.find((question) => question.id === activeQuestionId) ?? null;
    }, [activeQuestionId, questions]);
    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        }),
    );

    const handleDragStart = (event: DragStartEvent) => {
        setActiveQuestionId(String(event.active.id));
    };

    const handleDragEnd = (event: DragEndEvent) => {
        const { active, over } = event;
        setActiveQuestionId(null);

        if (!over || active.id === over.id) return;

        const activeQuestionId = String(active.id);
        const overQuestionId = String(over.id);
        const overQuestion = questions.find((question) => question.id === overQuestionId);
        if (!overQuestion) return;

        const previousQuestions = questions.map(cloneQuestion);

        dispatch(
            reorderQuestions({
                pageIndex,
                activeQuestionId,
                overQuestionId,
            }),
        );

        updateQuestion(activeQuestionId, { serialNumber: overQuestion.serialNumber }).catch(() => {
            dispatch(setErrorMessage({ message: 'Не удалось изменить порядок вопросов' }));
            dispatch(setPageQuestions({ pageIndex, questions: previousQuestions }));
        });
    };

    return (
        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            autoScroll
            onDragStart={handleDragStart}
            onDragCancel={() => setActiveQuestionId(null)}
            onDragEnd={handleDragEnd}
        >
            <SortableContext items={questionIds} strategy={verticalListSortingStrategy}>
                <div className={style.container}>
                    {questions.map((question) => (
                        <SortableQuestion
                            key={question.id}
                            question={question}
                            onClick={() => dispatch(setSelectedQuestion({ question, pageIndex }))}
                            isEditMode={editQuestionId === question.id}
                        />
                    ))}
                </div>
            </SortableContext>
            <DragOverlay>
                {activeQuestion ? (
                    <QuestionComponent
                        question={activeQuestion}
                        isEditMode={editQuestionId === activeQuestion.id}
                        isDragOverlay
                    />
                ) : null}
            </DragOverlay>
        </DndContext>
    );
}
