import { useParams } from 'react-router-dom';

export function SurveyView() {
    const { id } = useParams();
    return <div>Просмотр опроса - ID: {id}</div>;
}
