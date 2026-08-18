import { useState } from 'react';
import { createQuestion } from '@/api/question';
import { addQuestion } from '@/entities/Pages/Pages.slice';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import style from './AddQuestionCard.module.css';

type Props = {
    pageId: string;
    pageIndex: number;
    serialNumber: number;
};

export function AddQuestionCard({ pageId, pageIndex, serialNumber }: Props) {
    const dispatch = useAppDispatch();
    const [isCreating, setIsCreating] = useState(false);

    const addQuestionHandler = async () => {
        if (isCreating) return;

        setIsCreating(true);

        try {
            const question = await createQuestion(pageId, {
                text: 'Новый вопрос',
                serialNumber,
                type: 'SHORT_TEXT',
            });
            dispatch(addQuestion({ question, pageIndex }));
        } catch {
            dispatch(setErrorMessage({ message: 'Не удалось добавить вопрос' }));
        } finally {
            setIsCreating(false);
        }
    };

    return (
        <button className={style.card} type='button' aria-busy={isCreating} onClick={() => void addQuestionHandler()}>
            <span className={style.plus} aria-hidden='true'>
                +
            </span>
            <span className={style.label}>Добавить вопрос</span>
        </button>
    );
}
