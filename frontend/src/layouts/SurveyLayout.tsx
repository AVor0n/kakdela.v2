import { Link, Outlet, useLocation, useParams } from 'react-router-dom';
import { routes } from '@/app/routes';
import './SurveyLayout.css';
import { Link as LinkHH, Button } from '@hh.ru/magritte-ui';
export function SurveyLayout() {
    const { id } = useParams();
    const basePath = id ? routes.surveyEdit(id) : routes.surveyCreate();
    const { pathname } = useLocation();
    return (
        <>
            <header className='header__surveys_actions'>
                <LinkHH mode='primary' style='accent' href={routes.survey()}>
                    Обратно в меню
                </LinkHH>
                <nav className='navbar'>
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
            </header>
            <Outlet />
        </>
    );
}
