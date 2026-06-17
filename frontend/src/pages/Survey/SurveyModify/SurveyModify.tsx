// import { useParams } from 'react-router-dom';

import { SurveyDetail } from './components/SurveyDetail/SurveyDetail';
import { QuestionList } from './components/QuestionList/QuestionList';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useEffect } from 'react';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { mockSurvey } from '@/shared/mock/Survey.mock';
import { Sidebar } from './components/Sidebar/Sidebar';

import style from './SurveyModify.module.css';

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
        <div className={style.container}>
            <div className={style.content}>
                <SurveyDetail survey={selectedSurvey!} />
                {/* <div>Редактирование опроса - ID: {id}</div> */}
                <QuestionList questions={selectedSurvey!.pages[0].questions} />
            </div>
            <Sidebar />
        </div>
    );
}
