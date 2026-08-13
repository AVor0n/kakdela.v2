import { getPublicSurveyById, getSurveyForEditById } from '@/api/survey';
import type { Survey, SurveyPublic } from '@/shared/types/Survey.type';
import { getAccountDetails } from '@/api/account';
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { routes } from '@/app/routes';
import { SurveyRunner, type SurveyRunnerMode } from './components/SurveyRunner/SurveyRunner';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { AccountDetail } from '@/shared/ui/AccountDetail/AccountDetail';
import { ProductLogo } from '@/shared/ui/ProductLogo/ProductLogo';
import { setAccount, clearAccount, setLoading } from '@/entities/Account/Account.slice';
import style from './SurveyView.module.css';

type LoadedSurvey = { mode: 'preview'; survey: Survey } | { mode: 'respond'; survey: SurveyPublic };

export function SurveyView() {
    const { id } = useParams();
    const [searchParams] = useSearchParams();
    const [loadedSurvey, setLoadedSurvey] = useState<LoadedSurvey | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const mode: SurveyRunnerMode = searchParams.get('preview') === 'true' ? 'preview' : 'respond';
    const { account } = useAppSelector((state) => state.account);
    const [isAccountChecked, setIsAccountChecked] = useState(false);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const location = useLocation();
    useEffect(() => {
        if (!id) {
            setError('Опрос не найден');
            setIsLoading(false);
            return;
        }

        let isActive = true;
        setIsLoading(true);
        setLoadedSurvey(null);
        const surveyRequest: Promise<LoadedSurvey> =
            mode === 'preview'
                ? getSurveyForEditById(id).then((survey) => ({ mode, survey }))
                : getPublicSurveyById(id).then((survey) => ({ mode, survey }));
        surveyRequest
            .then((data) => {
                if (!isActive) return;
                setLoadedSurvey(data);
                setError(null);
            })
            .catch(() => {
                if (isActive) setError('Не удалось загрузить опрос');
            })
            .finally(() => {
                if (isActive) setIsLoading(false);
            });

        return () => {
            isActive = false;
        };
    }, [id, mode]);

    const survey = loadedSurvey?.survey ?? null;

    useEffect(() => {
        if (isAccountChecked && !account && survey && survey.isAuthorizedOnly) {
            dispatch(setErrorMessage({ message: 'Этот опрос только для зарегистрированных пользователей' }));
            navigate(routes.login(), { state: { from: location } });
        }
    }, [account, dispatch, isAccountChecked, location, mode, navigate, survey]);

    useEffect(() => {
        if (!survey) return;

        if (account) {
            setIsAccountChecked(true);
            return;
        }

        getAccountDetails()
            .then((data) => dispatch(setAccount(data)))
            .catch(() => dispatch(clearAccount()))
            .finally(() => {
                setIsAccountChecked(true);
                dispatch(setLoading(false));
            });
    }, [survey, account, dispatch]);

    if (isLoading) {
        return <div>Загрузка...</div>;
    }

    if (error || !loadedSurvey) {
        return (
            <div>
                <p>{error ?? 'Опрос не найден'}</p>
                <Link to={routes.survey()}>Вернуться к списку опросов</Link>
            </div>
        );
    }

    return (
        <div className={style.page}>
            <header className={style.header}>
                <ProductLogo to={routes.root()} className={style.productLogo} />
                <div className={style.accountState}>
                    {isAccountChecked && account ? (
                        <AccountDetail />
                    ) : (
                        <p className={style.anonymousBadge}>Анонимное прохождение</p>
                    )}
                </div>
            </header>
            {loadedSurvey.mode === 'preview' ? (
                <SurveyRunner key={`${loadedSurvey.survey.id}-preview`} survey={loadedSurvey.survey} mode='preview' />
            ) : (
                <SurveyRunner key={`${loadedSurvey.survey.id}-respond`} survey={loadedSurvey.survey} mode='respond' />
            )}
        </div>
    );
}
