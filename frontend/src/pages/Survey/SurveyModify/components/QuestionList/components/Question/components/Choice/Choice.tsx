import {
    addQuestionOptions,
    reorderAnswerOptions,
    setQuestionAnswerOptions,
} from '@/entities/Survey/Survey.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { Checkbox, Link, Radio } from '@hh.ru/magritte-ui';
import { addAnswerOption, updateAnswerOption } from '@/api/answer-option';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { AnswerOption } from '@/shared/types/Question.type';
import { Option } from './components/Option/Option';
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
import { SortableOption } from './components/SortableOption/SortableOption';
import style from './Choice.module.css';
interface Props {
    options: AnswerOption[];
    type: 'radio' | 'checkbox';
    isEdit: boolean;
}

export function Choice({ options, type, isEdit }: Props) {
    const { selectedSurvey, selectedQuestion } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();
    const optionIds = useMemo(() => options.map((option) => option.id), [options]);
    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        }),
    );

    const renderChoiceControl = () => {
        return type === 'checkbox' ? (
            <Checkbox name='multiple_choice' checked={false} onChange={() => {}} />
        ) : (
            <Radio name='single_choice' checked={false} onChange={() => {}} />
        );
    };

    const handleOptionDragEnd = (event: DragEndEvent) => {
        if (!selectedQuestion) return;

        const { active, over } = event;
        if (!over || active.id === over.id) return;

        const activeOptionId = String(active.id);
        const overOptionId = String(over.id);
        const overOption = options.find((option) => option.id === overOptionId);
        if (!overOption) return;

        const previousOptions = options.map((option) => ({ ...option }));

        dispatch(
            reorderAnswerOptions({
                questionId: selectedQuestion.id,
                activeOptionId,
                overOptionId,
            }),
        );

        updateAnswerOption(activeOptionId, { serialNumber: overOption.serialNumber }).catch(() => {
            dispatch(setErrorMessage({ message: 'Не удалось изменить порядок вариантов ответа' }));
            dispatch(setQuestionAnswerOptions({ questionId: selectedQuestion.id, answerOptions: previousOptions }));
        });
    };

    const createAnswerOptionHandler = () => {
        if (!selectedSurvey || !selectedQuestion) return;
        let serialNumber = 1;
        if (options.length !== 0) {
            const lastAnswerOptionSerialNumber = options[options.length - 1].serialNumber;
            serialNumber = lastAnswerOptionSerialNumber + 1;
        }

        addAnswerOption(selectedQuestion.id, {
            answerOptionText: `Вопрос ${serialNumber}`,
            serialNumber: serialNumber,
        })
            .then((data) => {
                dispatch(addQuestionOptions({ answerOption: data }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: `Не удалось добавить новый ответ 'Вопрос ${serialNumber}'` }));
                }
            });
    };

    const createAnotherOptionHandler = () => {
        if (!selectedSurvey || !selectedQuestion) return;
        let serialNumber = 1;
        if (options.length === 0) {
            const lastAnswerOptionSerialNumber = options[options.length - 1].serialNumber;
            serialNumber = lastAnswerOptionSerialNumber + 1;
        }

        addAnswerOption(selectedQuestion.id, {
            answerOptionText: 'Другое',
            serialNumber: serialNumber,
        })
            .then((data) => {
                dispatch(addQuestionOptions({ answerOption: data }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: `Не удалось добавить новый ответ 'Другое'` }));
                }
            });
    };

    return (
        <div className={style.container}>
            {isEdit ? (
                <DndContext
                    sensors={sensors}
                    collisionDetection={closestCenter}
                    autoScroll
                    onDragEnd={handleOptionDragEnd}
                >
                    <SortableContext items={optionIds} strategy={verticalListSortingStrategy}>
                        {options.map((option) => (
                            <SortableOption option={option} isEdit={isEdit} key={option.id}>
                                {renderChoiceControl()}
                            </SortableOption>
                        ))}
                    </SortableContext>
                </DndContext>
            ) : (
                options.map((option) => (
                    <Option option={option} isEdit={isEdit} key={option.id}>
                        {renderChoiceControl()}
                    </Option>
                ))
            )}
            {isEdit && (
                <div className={style.add}>
                    <div className={style.actions}>
                        <Link Element='button' mode='secondary' style='accent' onClick={createAnswerOptionHandler}>
                            Добавить ответ
                        </Link>
                        <span>или</span>
                        <Link Element='button' mode='secondary' style='accent' onClick={createAnotherOptionHandler}>
                            Добавить вариант "Другое"
                        </Link>
                    </div>
                </div>
            )}
        </div>
    );
}
