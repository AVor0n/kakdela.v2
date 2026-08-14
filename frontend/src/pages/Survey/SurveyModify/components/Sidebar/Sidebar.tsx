import { Box, Button } from '@hh.ru/magritte-ui';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { addPage, addQuestion } from '@/entities/Pages/Pages.slice';
import { useAppSelector } from '@/hooks/useAppSelector';
import { createSurveyPage } from '@/api/surveyPages';
import { createQuestion } from '@/api/question';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './Sidebar.module.css';

export function Sidebar() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const { selectedTemplate } = useAppSelector((state) => state.template);
    const { pages } = useAppSelector((state) => state.pages);
    const { currentQuestionPageIndex } = useAppSelector((state) => state.pages);
    const dispatch = useAppDispatch();

    const addQuestionHandler = () => {
        let id;
        if (selectedSurvey) {
            id = selectedSurvey.id;
        } else if (selectedTemplate) {
            id = selectedTemplate.id;
        } else {
            return;
        }
        if (pages.length === 0) {
            createSurveyPage(id, 1)
                .then((data) => {
                    dispatch(addPage({ page: data }));

                    return createQuestion(data.id, { text: 'Новый вопрос', serialNumber: 1, type: 'SHORT_TEXT' });
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
            const serialNumber = pages[currentQuestionPageIndex].questions.length + 1;
            createQuestion(pages[currentQuestionPageIndex].id, {
                text: 'Новый вопрос',
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
        let id;
        if (selectedSurvey) {
            id = selectedSurvey.id;
        } else if (selectedTemplate) {
            id = selectedTemplate.id;
        } else {
            return;
        }
        let serialNumber = pages[pages.length - 1] ? pages[pages.length - 1].serialNumber + 1 : 1;
        if (pages.length === 0) {
            serialNumber = 1;
        }
        createSurveyPage(id, serialNumber)
            .then((data) => {
                dispatch(addPage({ page: data }));
                return createQuestion(data.id, { text: 'Новый вопрос', serialNumber: 1, type: 'SHORT_TEXT' });
            })
            .then((question) => {
                dispatch(addQuestion({ question, pageIndex: pages.length }));
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
