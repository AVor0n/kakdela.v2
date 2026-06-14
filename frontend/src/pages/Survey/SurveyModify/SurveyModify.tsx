// import { useParams } from 'react-router-dom';

import './SurveyModify.css';
import { SurveyDetail } from './components/SurveyDetail/SurveyDetail';
import { QuestionList } from './components/QuestionList/QuestionList';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useEffect } from 'react';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { mockSurvey } from '@/shared/mock/Survey.mock';
export function SurveyModify() {
    // const { id } = useParams();
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();
    // TODO: Логика получения данных опроса по id

    useEffect(() => {
        dispatch(setSelectedSurvey({ survey: mockSurvey }));
    }, []);

    if (!selectedSurvey) {
        return <div>Загрузка...</div>;
    }
    return (
        <div className='survey__constructor'>
            <div className='survey__constructor_container'>
                <SurveyDetail survey={selectedSurvey!} />
                {/* <div>Редактирование опроса - ID: {id}</div> */}
                <QuestionList questions={selectedSurvey!.pages[0].questions} />
            </div>
        </div>
    );
}
