import { Outlet, useParams } from 'react-router-dom';

export function SurveyModify() {
    const { id } = useParams();
    return (
        <>
            <div>Редактирование опроса - ID: {id}</div>
            <Outlet />
        </>
    );
}
