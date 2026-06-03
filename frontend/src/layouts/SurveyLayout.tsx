import { Link, Outlet, useParams } from 'react-router-dom';
import { routes } from '@/app/routes';

export function SurveyLayout() {
    const { id } = useParams();
    const basePath = id ? routes.surveyEdit(id) : routes.surveyCreate();
    return (
        <>
            <nav style={{ display: 'flex', gap: '1rem' }}>
                <Link to={`${basePath}/questions`}>Вопросы</Link>
                <Link to={`${basePath}/answers`}>Ответы</Link>
                <Link to={`${basePath}/settings`}>Настройки</Link>
            </nav>
            <Outlet />
        </>
    );
}
