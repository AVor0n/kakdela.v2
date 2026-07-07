import { getSurveyById } from '@/api/survey';
import type { Survey } from '@/shared/types/Survey.type';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { routePatterns, routes } from '@/app/routes';
import { SurveyRunner, type SurveyRunnerMode } from './components/SurveyRunner/SurveyRunner';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { clearErrorMessage, setErrorMessage } from '@/entities/Error/Error.slice';

export function SurveyView() {
    const { id } = useParams();
    const [searchParams] = useSearchParams();
    const [survey, setSurvey] = useState<Survey | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const mode: SurveyRunnerMode = searchParams.get('preview') === 'true' ? 'preview' : 'respond';

    useEffect(() => {
        if (!id) {
            dispatch(setErrorMessage({ message: 'Опрос не найден' }));
            setIsLoading(false);
            navigate(routePatterns.notFound);
            return;
        }

        setIsLoading(true);
        getSurveyById(id)
            .then((data) => {
                setSurvey(data);
                dispatch(clearErrorMessage());
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось загрузить опрос' }));
                navigate(routePatterns.notFound);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [id]);

    if (isLoading) {
        return <div>Загрузка...</div>;
    }

    if (!survey) {
        return (
            <div>
                <Link to={routes.survey()}>Вернуться к списку опросов</Link>
            </div>
        );
    }

    return <SurveyRunner survey={survey} mode={mode} />;
}
