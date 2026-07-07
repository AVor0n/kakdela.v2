import { Box, Button } from '@hh.ru/magritte-ui';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { addPage, addQuestion } from '@/entities/Survey/Survey.slice';
import { useAppSelector } from '@/hooks/useAppSelector';
import { createSurveyPage } from '@/api/surveyPages';
import { createQuestion } from '@/api/question';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './Sidebar.module.css';

export function Sidebar() {
    const { selectedSurvey, currentQuestionPageIndex } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();

    const addQuestionHandler = () => {
        if (!selectedSurvey) return;
        if (selectedSurvey.pages.length === 0) {
            createSurveyPage(selectedSurvey.id, 1)
                .then((data) => {
                    dispatch(addPage({ page: data }));

                    return createQuestion(data.id, { title: 'Новый вопрос', serialNumber: 1, type: 'SHORT_TEXT' });
                })
                .then((question) => {
                    dispatch(addQuestion({ question }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: `Не удалось добавить новую страницу опроса` }));
                    }
                });
        } else {
            const serialNumber = selectedSurvey.pages[currentQuestionPageIndex].questions.length + 1;
            createQuestion(selectedSurvey.pages[currentQuestionPageIndex].id, {
                title: 'Новый вопрос',
                serialNumber: serialNumber,
                type: 'SHORT_TEXT',
            })
                .then((data) => {
                    dispatch(addQuestion({ question: data }));
                })
                .catch((err) => {
                    if (err.response) {
                        dispatch(setErrorMessage({ message: `Не удалось добавить вопрос` }));
                    }
                });
        }
    };

    const addPageHandler = () => {
        if (!selectedSurvey) return;
        let serialNumber = selectedSurvey.pages[selectedSurvey.pages.length - 1]
            ? selectedSurvey.pages[selectedSurvey.pages.length - 1].serialNumber + 1
            : 1;
        if (selectedSurvey.pages.length === 0) {
            serialNumber = 1;
        }
        createSurveyPage(selectedSurvey.id, serialNumber)
            .then((data) => {
                dispatch(addPage({ page: data }));

                return createQuestion(data.id, { title: 'Новый вопрос', serialNumber: 1, type: 'SHORT_TEXT' });
            })
            .then((question) => {
                dispatch(addQuestion({ question, pageIndex: selectedSurvey.pages.length }));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: `Не удалось добавить новую страницу` }));
                }
            });
    };

    return (
        <Box height='fit-content' className={style.container}>
            <Button
                mode='secondary'
                type='button'
                icon={<img src='/add.svg' alt='add question' />}
                onClick={() => dispatch(addQuestionHandler)}
            />
            <Button
                mode='secondary'
                type='button'
                icon={<img src='/add-page.svg' alt='add page' />}
                onClick={() => dispatch(addPageHandler)}
            />
        </Box>
    );
}
