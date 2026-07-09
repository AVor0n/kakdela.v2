import { useEffect, useState } from 'react';
import style from './ErrorBlock.module.css';
import { clearErrorMessage, type IErrorState } from '@/entities/Error/Error.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
interface Props {
    error: IErrorState;
}

const CLOSE_DELAY = 2000;
const CLOSE_ANIMATION_DURATION = 300;

export function ErrorBlock({ error }: Props) {
    const [isClosing, setIsClosing] = useState(false);
    const dispatch = useAppDispatch();

    useEffect(() => {
        setIsClosing(false);
        const closeTimeout = setTimeout(() => {
            setIsClosing(true);
        }, CLOSE_DELAY);

        return () => {
            clearTimeout(closeTimeout);
        };
    }, [error]);

    useEffect(() => {
        if (!isClosing) {
            return;
        }
        const removeTimeout = setTimeout(() => {
            dispatch(clearErrorMessage());
        }, CLOSE_ANIMATION_DURATION);

        return () => {
            clearTimeout(removeTimeout);
        };
    }, [isClosing, dispatch]);

    return (
        <div className={`${style.container} ${isClosing ? style.closing : ''}`}>
            <p className={style.content}>{error.message}</p>
            <img src='/X.svg' alt='X' onClick={() => dispatch(clearErrorMessage())} />
        </div>
    );
}
