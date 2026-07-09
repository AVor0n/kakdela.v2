// import { useParams } from 'react-router-dom';

import { SurveyDetail } from './components/SurveyDetail/SurveyDetail';
import { QuestionList } from './components/QuestionList/QuestionList';
import { useAppSelector } from '@/hooks/useAppSelector';
import { Sidebar } from './components/Sidebar/Sidebar';

import style from './SurveyModify.module.css';
import { PageSeparator } from './components/PageSeparator/PageSeparator';

export function SurveyModify() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);

    if (!selectedSurvey) {
        return <div>Загрузка...</div>;
    }
    return (
        <div className={style.container}>
            <div className={style.content}>
                <SurveyDetail survey={selectedSurvey} />
                {selectedSurvey.pages.map((page, index) => {
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
