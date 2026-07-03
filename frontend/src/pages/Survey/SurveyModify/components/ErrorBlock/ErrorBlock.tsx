import type { Error } from '@/shared/types/Error.type';
import type { Dispatch, SetStateAction } from 'react';
import { useEffect, useState } from 'react';
import style from './ErrorBlock.module.css';
interface Props {
    error: Error;
    setError: Dispatch<SetStateAction<Error | null>>;
}

const CLOSE_DELAY = 2000;
const CLOSE_ANIMATION_DURATION = 300;

export function ErrorBlock({ error, setError }: Props) {
    const [isClosing, setIsClosing] = useState(false);

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
            setError(null);
        }, CLOSE_ANIMATION_DURATION);

        return () => {
            clearTimeout(removeTimeout);
        };
    }, [isClosing, setError]);

    return (
        <div className={`${style.container} ${isClosing ? style.closing : ''}`}>
            <p className={style.content}>{error.message}</p>
            <img src='/X.svg' alt='X' onClick={() => setError(null)} />
        </div>
    );
}
