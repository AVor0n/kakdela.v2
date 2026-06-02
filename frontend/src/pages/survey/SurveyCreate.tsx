import { Outlet } from 'react-router-dom';

export function SurveyCreate() {
    return (
        <>
            <div>Создание опроса</div>
            <Outlet />
        </>
    );
}
