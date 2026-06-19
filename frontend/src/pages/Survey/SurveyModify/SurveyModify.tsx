// import { useParams } from 'react-router-dom';

import { SurveyDetail } from './components/SurveyDetail/SurveyDetail';
import { QuestionList } from './components/QuestionList/QuestionList';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useEffect } from 'react';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setSelectedSurvey } from '@/entities/Survey/Survey.slice';
import { Sidebar } from './components/Sidebar/Sidebar';

import style from './SurveyModify.module.css';
import { getSurveyById } from '@/api/survey';
import { useParams } from 'react-router-dom';

export function SurveyModify() {
    const { id } = useParams();
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();
    // TODO: Логика получения данных опроса по id

    useEffect(() => {
        if (id) {
            getSurveyById(id).then((data) => dispatch(setSelectedSurvey({ survey: data })));
        }
    }, []);

    if (!selectedSurvey) {
        return <div>Загрузка...</div>;
    }
    return (
        <div className={style.container}>
            <div className={style.content}>
                <SurveyDetail survey={selectedSurvey!} />
                {selectedSurvey!.pages.map((page, index) => {
                    return (
                        <div key={index} className={style.page}>
                            <QuestionList questions={page.questions} pageIndex={index} />
                            <div>page {page.serialNumber}</div>
                        </div>
                    );
                })}
            </div>
            <Sidebar />
        </div>
    );
}
