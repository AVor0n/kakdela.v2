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
import { PageSeparator } from './components/PageSeparator/PageSeparator';

export function SurveyModify() {
    const { id } = useParams();
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();

    useEffect(() => {
        if (!id) {
            return;
        }

        getSurveyById(id).then((data) => {
            dispatch(setSelectedSurvey({ survey: data }));
        });
    }, [dispatch, id]);

    if (!selectedSurvey) {
        return <div>Загрузка...</div>;
    }
    return (
        <div className={style.container}>
            <div className={style.content}>
                <SurveyDetail survey={selectedSurvey} />
                {selectedSurvey!.pages.map((page, index) => {
                    return (
                        <div key={page.id}>
                            <PageSeparator page={page} />
                            <QuestionList questions={page.questions} pageNumber={page.serialNumber} pageIndex={index} />
                        </div>
                    );
                })}
            </div>
            <Sidebar />
        </div>
    );
}
