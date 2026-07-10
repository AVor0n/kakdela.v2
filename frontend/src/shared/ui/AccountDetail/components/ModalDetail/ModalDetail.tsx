import { Loader } from '@hh.ru/magritte-ui';
import { useAppSelector } from '@/hooks/useAppSelector';
import { UserCircleOutlinedSize24, LineArrowRightOutlinedSize24 } from '@hh.ru/magritte-ui/icon';

import style from './ModalDetail.module.css';
import { logout } from '@/api/account';
import { useNavigate } from 'react-router-dom';
import { routePatterns } from '@/app/routes';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setErrorMessage } from '@/entities/Error/Error.slice';

interface Props {
    onMouseLeave: () => void;
}
export function ModalDetail({ onMouseLeave }: Props) {
    const { account, loading } = useAppSelector((state) => state.account);
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const logoutHandler = () => {
        logout()
            .then(() => {
                navigate(routePatterns.root);
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось выйти с аккаунта' }));
            });
    };

    const profileHandler = () => {
        dispatch(
            setErrorMessage({
                message: 'Страницы профиля ещё нету, но совсем скоро она появится, мы работаем над этим❤️',
            }),
        );
    };
    return (
        <div className={style.modal} onMouseLeave={onMouseLeave}>
            {loading ? (
                <Loader size={32} />
            ) : (
                <>
                    <div>
                        <p className={style.login}>{account!.login}</p>
                        <p className={style.email}>{account!.email}</p>
                    </div>
                    <div className={style.separator} />
                    <div className={style.menu}>
                        <button className={style.button} onClick={profileHandler}>
                            <UserCircleOutlinedSize24 />
                            <span>Профиль</span>
                        </button>
                        <div className={style.separator} />
                        <button className={style.button} onClick={logoutHandler}>
                            <LineArrowRightOutlinedSize24 />
                            <span>Выйти</span>
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}
