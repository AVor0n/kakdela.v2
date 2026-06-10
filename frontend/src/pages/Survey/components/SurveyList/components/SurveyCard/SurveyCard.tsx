import type { Survey } from '@/shared/types/Survey.type';
import './SurveyCard.css';
import { Link } from 'react-router-dom';
import { routes } from '@/app/routes';
interface Props {
    survey: Survey;
}

export function SurveyCard({ survey }: Props) {
    return (
        <Link to={routes.surveyEdit(survey.id)} className='card'>
            <div className='card__block'></div>
            <h2 className='card__title'>{survey.title}</h2>
        </Link>
    );
}
