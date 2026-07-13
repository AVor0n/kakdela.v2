import { getSurveyById } from '@/api/survey';
import type { Survey } from '@/shared/types/Survey.type';
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { routePatterns, routes } from '@/app/routes';
import { SurveyRunner, type SurveyRunnerMode } from './components/SurveyRunner/SurveyRunner';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setErrorMessage } from '@/entities/Error/Error.slice';

export function SurveyView() {
    const { id } = useParams();
    const [searchParams] = useSearchParams();
    const [survey, setSurvey] = useState<Survey | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const mode: SurveyRunnerMode = searchParams.get('preview') === 'true' ? 'preview' : 'respond';
    const { account } = useAppSelector((state) => state.account);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const location = useLocation();
    useEffect(() => {
        if (!id) {
            setError('Опрос не найден');
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        getSurveyById(id)
            .then((data) => {
                setSurvey(data);
                setError(null);
            })
            .catch(() => {
                setError('Не удалось загрузить опрос');
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [id]);

    useEffect(() => {
        if (survey && !survey.isPublished) {
            dispatch(setErrorMessage({ message: 'Опрос не существует или ещё не опубликован' }));
            navigate(routePatterns.notFound);
        }
        if (!account && survey && survey.isAuthorizedOnly) {
            dispatch(setErrorMessage({ message: 'Этот опрос только для зарегистрированных пользователей' }));
            navigate(routes.login(), { state: { from: location } });
        }
    }, [survey]);

    if (isLoading) {
        return <div>Загрузка...</div>;
    }

    if (error || !survey) {
        return (
            <div>
                <p>{error ?? 'Опрос не найден'}</p>
                <Link to={routes.survey()}>Вернуться к списку опросов</Link>
            </div>
        );
    }

    return <SurveyRunner survey={survey} mode={mode} />;
}
