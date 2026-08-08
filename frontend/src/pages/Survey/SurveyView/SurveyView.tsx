import { getSurveyById } from '@/api/survey';
import type { Survey } from '@/shared/types/Survey.type';
import { getAccountDetails } from '@/api/account';
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { routePatterns, routes } from '@/app/routes';
import { SurveyRunner, type SurveyRunnerMode } from './components/SurveyRunner/SurveyRunner';
import { useAppSelector } from '@/hooks/useAppSelector';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { AccountDetail } from '@/shared/ui/AccountDetail/AccountDetail';
import { ProductLogo } from '@/shared/ui/ProductLogo/ProductLogo';
import { setAccount, clearAccount, setLoading } from '@/entities/Account/Account.slice';
import style from './SurveyView.module.css';
export function SurveyView() {
    const { id } = useParams();
    const [searchParams] = useSearchParams();
    const [survey, setSurvey] = useState<Survey | null>(null);
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
        if (survey && !survey.isPublished && mode !== 'preview') {
            dispatch(setErrorMessage({ message: 'Опрос не существует или ещё не опубликован' }));
            navigate(routePatterns.notFound);
        }
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

    if (error || !survey) {
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
            <SurveyRunner survey={survey} mode={mode} />
        </div>
    );
}
