import { Outlet } from 'react-router-dom';

export function SurveyLayout() {
    return (
        <>
            <nav style={{ display: 'flex', gap: '1rem' }}>
                <a href='questions'>Вопросы</a>
                <a href='answers'>Ответы</a>
                <a href='settings'>Настройки</a>
            </nav>
            <Outlet />
        </>
    );
}
