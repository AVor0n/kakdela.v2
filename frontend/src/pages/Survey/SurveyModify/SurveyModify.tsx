import { mockSurvey } from '@/shared/mock/Survey.mock';
import type { Survey } from '@/shared/types/Survey.type';
import { useState } from 'react';
// import { useParams } from 'react-router-dom';

import './SurveyModify.css';
import { SurveyDetail } from './components/SurveyDetail/SurveyDetail';
import { QuestionList } from './components/QuestionList/QuestionList';
export function SurveyModify() {
    // const { id } = useParams();
    const [survey, setSurvey] = useState<Survey>(mockSurvey); // TODO: Изначально null, после загрузки данных - объект опроса
    // TODO: Логика получения данных опроса по id
    return (
        <div className='survey__constructor'>
            <div className='survey__constructor_container'>
                <SurveyDetail survey={survey!} setSurvey={setSurvey} />
                {/* <div>Редактирование опроса - ID: {id}</div> */}
                <QuestionList questions={survey!.pages[0].questions} />
            </div>
        </div>
    );
}
