import { useEffect, useState } from 'react';
import style from './LoadingContent.module.css';

export function LoadingContent() {
    const [text, setText] = useState<string>('Загрузка');

    useEffect(() => {
        const interval = setInterval(() => {
            setText((prev) => {
                // Если точек стало 4 (максимум 3 точки + исходный текст), сбрасываем до базового текста
                if (prev.length >= 'Загрузка'.length + 3) {
                    return 'Загрузка';
                }
                // Иначе добавляем одну точку
                return prev + '.';
            });
        }, 500);

        return () => {
            clearInterval(interval);
        };
    }, []);
    return (
        <div className={style.container}>
            <p className={style.content}>{text}</p>
        </div>
    );
}
