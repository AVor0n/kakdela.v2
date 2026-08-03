import type { AnswerOptionOrder, Question, QuestionType } from '@/shared/types/Question.type';
import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core';
import { Button, Checkbox, createStaticDataProvider, Select, type StaticDataFetcherItem } from '@hh.ru/magritte-ui';
import { useCallback, useEffect, useMemo, useState, type ChangeEvent, type Ref } from 'react';
import { ShortText } from './components/ShortText/ShortText';
import { LongText } from './components/LongText/LongText';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import {
    deleteQuestion as deleteQuestionState,
    duplicateQuestion,
    setMandatory as setMandatoryState,
    setQuestion,
    updateAnswerOptionOrder,
    updateQuestionDescription,
    updateQuestionTitle,
    updateQuestionType,
} from '@/entities/Survey/Survey.slice';
import { Choice } from './components/Choice/Choice';
import classNames from 'classnames';
import { useDebounce } from '@/hooks/useDebounce';
import { cloneQuestion, deleteQuestion, updateQuestion } from '@/api/question';
import { useAppSelector } from '@/hooks/useAppSelector';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { attachImageToQuestion, removeImageFromQuestion, updateAttachmentOfQuestion } from '@/api/attachments';
import style from './Question.module.css';
import { DragHandle } from './components/QuestionControls/DragHandle/DragHandle';
import { EditorInput } from '@/shared/ui/EditorInput/EditorInput';

interface Props {
    question: Question;
    isEditMode?: boolean;
    isDragging?: boolean;
    isDragOverlay?: boolean;
    dragHandleAttributes?: DraggableAttributes;
    dragHandleListeners?: DraggableSyntheticListeners;
    dragHandleRef?: Ref<HTMLDivElement>;
    onClick?: () => void;
}

const OPTIONS: StaticDataFetcherItem[] = [
    { value: 'SHORT_TEXT', text: 'Короткий текст' },
    { value: 'LONG_TEXT', text: 'Длинный текст' },
    { value: 'SINGLE_CHOICE', text: 'Один из списка' },
    { value: 'MULTIPLE_CHOICE', text: 'Несколько из списка' },
];

const ANSWER_OPTION_ORDER: StaticDataFetcherItem[] = [
    { value: 'ORIGINAL', text: 'Обычный' },
    { value: 'RANDOM', text: 'Случайный' },
];

export function Question({
    question,
    onClick,
    dragHandleAttributes,
    dragHandleListeners,
    dragHandleRef,
    isEditMode = false,
    isDragging = false,
    isDragOverlay = false,
}: Props) {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const [title, setTitle] = useState<string>(question.title);
    const [typeQuestion, setTypeQuestion] = useState<QuestionType>(question.type);
    const [mandatory, setMandatory] = useState<boolean>(question.isMandatory);
    const [answerOptionOrder, setAnswerOptionOrder] = useState<AnswerOptionOrder>(
        question.answerOptionOrder ?? 'ORIGINAL',
    );
    const [file, setFile] = useState<File | null>(null);
    const [questionImage, setQuestionImage] = useState<string | null>(null);
    const [description, setDescription] = useState<string>(question.description ?? '');
    const debouncedMandatory = useDebounce(mandatory, 500);

    const dispatch = useAppDispatch();

    const updateQuestionTitleHandler = () => {
        if (title !== question.title) {
            updateQuestion(question.id, { title })
                .then((data) => {
                    dispatch(updateQuestionTitle({ id: question.id, title: data.title }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить название вопроса' }));
                    }
                    setTitle(question.title);
                });
        }
    };

    const updateQuestionDescriptionHandler = () => {
        if (description !== question.description) {
            updateQuestion(question.id, { description })
                .then((data) => {
                    dispatch(updateQuestionDescription({ id: question.id, description: data.description }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить описание вопроса' }));
                    }
                    setDescription(question.description ?? '');
                });
        }
    };

    const updateQuestionTypeHandler = () => {
        if (typeQuestion !== question.type) {
            updateQuestion(question.id, { type: typeQuestion })
                .then((data) => {
                    dispatch(
                        updateQuestionType({
                            id: question.id,
                            type: data.type,
                        }),
                    );
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить тип вопроса' }));
                    }
                    setTypeQuestion(question.type);
                });
        }
    };

    const updateAnswerOptionOrderHandler = () => {
        if (answerOptionOrder !== question.answerOptionOrder) {
            updateQuestion(question.id, { answerOptionOrder: answerOptionOrder })
                .then((data) => {
                    dispatch(
                        updateAnswerOptionOrder({
                            id: question.id,
                            answerOptionOrder: data.answerOptionOrder ?? answerOptionOrder,
                        }),
                    );
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить способ вывода вариантов ответов' }));
                    }
                    setAnswerOptionOrder(question.answerOptionOrder ?? 'ORIGINAL');
                });
        }
    };

    const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            setFile(e.target.files[0]);
        }
    };

    const cloneQuestionHandler = () => {
        cloneQuestion(question.id)
            .then((data) => {
                dispatch(duplicateQuestion({ afterQuestionId: question.id, question: data }));
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось клонировать вопрос' }));
            });
    };

    const deleteAttachmentUrlHandler = () => {
        removeImageFromQuestion(question.id)
            .then(() => {
                setQuestionImage(null);
                dispatch(setQuestion({ question: { ...question, attachmentUrl: null } }));
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось удалить изображение для вопроса' }));
            });
    };

    useEffect(() => {
        if (!file) return;
        if (!question.attachmentUrl)
            attachImageToQuestion(question.id, file)
                .then((data) => {
                    dispatch(setQuestion({ question: { ...question, attachmentUrl: data.attachmentUrl } }));
                    setQuestionImage(data.attachmentUrl);
                })
                .catch(() => dispatch(setErrorMessage({ message: 'Не удалось прикрепить изображение' })));
        else
            updateAttachmentOfQuestion(question.id, file)
                .then((data) => {
                    dispatch(setQuestion({ question: { ...question, attachmentUrl: data.attachmentUrl } }));
                    setQuestionImage(data.attachmentUrl);
                })
                .catch(() => dispatch(setErrorMessage({ message: 'Не удалось прикрепить изображение' })));
    }, [file]);

    useEffect(() => {
        if (!question.attachmentUrl) return;
        setQuestionImage(question.attachmentUrl);
    }, [question]);

    useEffect(() => {
        if (debouncedMandatory !== question.isMandatory) {
            updateQuestion(question.id, { isMandatory: mandatory })
                .then((data) => {
                    dispatch(setMandatoryState({ value: data.isMandatory }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: 'Не удалось изменить описание опроса' }));
                    }
                    dispatch(setMandatoryState({ value: question.isMandatory }));
                });
        }
    }, [debouncedMandatory]);

    const deleteQuestionHandler = () => {
        if (!selectedSurvey) return;
        deleteQuestion(question.id)
            .then(() => {
                dispatch(deleteQuestionState({ id: question.id }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: 'Не удалось удалить вопрос' }));
                }
            });
    };

    const questionType = useMemo(() => {
        return OPTIONS.find((option) => option.value === typeQuestion);
    }, [typeQuestion]);

    const answerOptionOrderType = useMemo(() => {
        return ANSWER_OPTION_ORDER.find((option) => option.value === answerOptionOrder);
    }, [answerOptionOrder]);

    const questionContent = useCallback(() => {
        switch (typeQuestion) {
            case 'SHORT_TEXT':
                return <ShortText />;
            case 'LONG_TEXT':
                return <LongText />;
            case 'SINGLE_CHOICE':
                return <Choice options={question.answerOptions!} isEdit={isEditMode} type='radio' />;
            case 'MULTIPLE_CHOICE':
                return <Choice options={question.answerOptions!} isEdit={isEditMode} type='checkbox' />;
            default:
                return null;
        }
    }, [question, typeQuestion, isEditMode]);

    return (
        <div
            className={classNames(style.container, {
                [style.edit]: isEditMode,
                [style.dragging]: isDragging,
                [style.dragOverlay]: isDragOverlay,
            })}
            onClick={onClick}
        >
            <DragHandle attributes={dragHandleAttributes} listeners={dragHandleListeners} setNodeRef={dragHandleRef} />
            {questionImage && <img src={questionImage} alt='img' className={style.attachmentUrl} />}
            <section className={style.settings}>
                <div className={style.questionDetail}>
                    <EditorInput
                        placeholder='Вопрос'
                        value={title}
                        onChange={setTitle}
                        onBlur={updateQuestionTitleHandler}
                        isTextColor
                    />
                </div>
                <div className={style.imageSettings}>
                    <input
                        type='file'
                        id={`file-upload-${question.id}`}
                        onChange={handleFileChange}
                        style={{ display: 'none' }} // Полностью скрываем стандартный вид
                    />

                    <label htmlFor={`file-upload-${question.id}`} className={style.button}>
                        <img src='/img.svg' alt='Выбрать файл' />
                    </label>

                    {questionImage && (
                        <button className={style.button} onClick={deleteAttachmentUrlHandler}>
                            <img src='/trash.svg' />
                        </button>
                    )}
                </div>
                <div>
                    <Select
                        type='radio'
                        value={questionType}
                        dataProvider={createStaticDataProvider(OPTIONS, 'Тип вопроса')}
                        name='area'
                        widthEqualToActivator={false}
                        dropWidth={270}
                        onChange={(e) => {
                            setTypeQuestion(e.value as QuestionType);
                        }}
                        onBlur={updateQuestionTypeHandler}
                    />
                </div>
            </section>
            <section className={style.questionDescription}>
                <EditorInput
                    placeholder='Описание вопроса'
                    value={description}
                    onChange={setDescription}
                    onBlur={updateQuestionDescriptionHandler}
                    isTextColor
                    isMarkColor
                    isHeading
                />
            </section>

            <section className={style.actions}>
                <div>{questionContent()}</div>
                <div
                    className={classNames(style.actionsContent, {
                        [style.hidden]: !isEditMode,
                        [style.visible]: isEditMode,
                    })}
                >
                    <label className={style.mandatoryCheckbox}>
                        <Checkbox checked={mandatory} onChange={() => setMandatory(!mandatory)} />
                        Обязательный
                    </label>
                    <Button
                        mode='secondary'
                        type='button'
                        icon={<img src='/copy.svg' alt='Дублировать' />}
                        onClick={cloneQuestionHandler}
                    />
                    <Button
                        mode='secondary'
                        style='negative'
                        type='button'
                        icon={<img src='/trash.svg' alt='Удалить' />}
                        onClick={deleteQuestionHandler}
                    />
                    {((questionType && questionType.value == 'SINGLE_CHOICE') ||
                        questionType?.value == 'MULTIPLE_CHOICE') && (
                        <div className={style.select}>
                            <Select
                                type='radio'
                                value={answerOptionOrderType}
                                dataProvider={createStaticDataProvider(ANSWER_OPTION_ORDER, 'Порядок ответов')}
                                widthEqualToActivator={false}
                                dropWidth={220}
                                name='area2'
                                onChange={(e) => {
                                    setAnswerOptionOrder(e.value as AnswerOptionOrder);
                                }}
                                onBlur={updateAnswerOptionOrderHandler}
                            />
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
}
