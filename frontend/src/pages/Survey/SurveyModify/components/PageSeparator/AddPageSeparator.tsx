import { useState } from 'react';
import { createQuestion } from '@/api/question';
import { createSurveyPage } from '@/api/surveyPages';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { addPage, addQuestion } from '@/entities/Pages/Pages.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { useAppSelector } from '@/hooks/useAppSelector';
import style from './PageSeparator.module.css';
import { PageSeparatorFrame } from './PageSeparatorFrame';

type Props = {
    surveyId: string;
};

export function AddPageSeparator({ surveyId }: Props) {
    const dispatch = useAppDispatch();
    const pages = useAppSelector((state) => state.pages.pages);
    const [isCreating, setIsCreating] = useState(false);

    const addPageHandler = async () => {
        if (isCreating) return;

        setIsCreating(true);
        const pageIndex = pages.length;
        const serialNumber = (pages.at(-1)?.serialNumber ?? 0) + 1;

        try {
            const page = await createSurveyPage(surveyId, serialNumber);
            dispatch(addPage({ page }));

            const question = await createQuestion(page.id, {
                text: 'Новый вопрос',
                serialNumber: 1,
                type: 'SHORT_TEXT',
            });
            dispatch(addQuestion({ question, pageIndex }));
        } catch {
            dispatch(setErrorMessage({ message: 'Не удалось добавить новую страницу' }));
        } finally {
            setIsCreating(false);
        }
    };

    return (
        <PageSeparatorFrame>
            <button
                className={style.addPageButton}
                type='button'
                disabled={isCreating}
                aria-busy={isCreating}
                onClick={() => void addPageHandler()}
            >
                Добавить страницу
            </button>
        </PageSeparatorFrame>
    );
}
