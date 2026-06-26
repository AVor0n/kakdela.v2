import { Link, Outlet, useLocation, useParams } from 'react-router-dom';
import { routes } from '@/app/routes';
import { Link as LinkHH, Button } from '@hh.ru/magritte-ui';
import style from './SurveyLayout.module.css';
import { updateSurvey } from '@/api/survey';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';

export function SurveyLayout() {
    const { id } = useParams();
    const basePath = id ? routes.surveyEdit(id) : routes.surveyCreate();
    const { pathname } = useLocation();
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();

    const publishingHandler = () => {
        if (id && selectedSurvey)
            updateSurvey(id, { isPublished: !selectedSurvey.isPublished }).then((data) => {
                dispatch(setSelectedSurvey({ survey: data }));
            });
    };

    return (
        <>
            <header className={style.header}>
                <LinkHH mode='primary' style='accent' href={routes.survey()}>
                    Обратно в меню
                </LinkHH>

                <nav className={style.navbar}>
                    <Button
                        mode={pathname.includes('/questions') ? 'primary' : 'secondary'}
                        style='accent'
                        Element={Link}
                        to={`${basePath}/questions`}
                    >
                        Вопросы
                    </Button>
                    <Button
                        mode={pathname.includes('/answers') ? 'primary' : 'secondary'}
                        style='accent'
                        Element={Link}
                        to={`${basePath}/answers`}
                    >
                        Ответы
                    </Button>
                    <Button
                        mode={pathname.includes('/settings') ? 'primary' : 'secondary'}
                        style='accent'
                        Element={Link}
                        to={`${basePath}/settings`}
                    >
                        Настройки
                    </Button>
                </nav>
                <div>
                    {id && (
                        <Button mode='secondary' style='neutral' Element={Link} to={routes.surveyPreview(id)}>
                            Предпросмотр
                        </Button>
                    )}
                    <Button mode='tertiary' style='accent' onClick={publishingHandler}>
                        {selectedSurvey?.isPublished ? 'Снять с публикации' : 'Опубликовать'}
                    </Button>
                </div>
            </header>

            <Outlet />
        </>
    );
}
