import { addSubscriber, deleteSubscriber, getSubscribers } from '@/api/subscribers';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { Subscribers } from '@/shared/types/Subscribers.type';
import { FormLabel, Input } from '@hh.ru/magritte-ui';
import { useEffect, useState, type KeyboardEvent } from 'react';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import style from './SubscribersInput.module.css';

export function SubscribersInput() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const dispatch = useAppDispatch();
    const [subscribers, setSubscribers] = useState<Subscribers[]>([]);
    const [subscribersInput, setSubscribersInput] = useState<string>('');

    useEffect(() => {
        getSubscribers(selectedSurvey!.id)
            .then((data) => setSubscribers(data))
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось получить подписчиков этого опроса',
                        }),
                    );
                }
            });
    }, []);

    const addSubscriberHandle = () => {
        addSubscriber(selectedSurvey!.id, subscribersInput)
            .then((data) => {
                if (data.alreadySubscribedEmails.length > 0) {
                    dispatch(
                        setErrorMessage({
                            message: 'Этот пользователь уже добавлен',
                        }),
                    );
                } else if (data.notFoundEmails.length > 0) {
                    dispatch(
                        setErrorMessage({
                            message: 'Пользователя с такой почтой не существует',
                        }),
                    );
                } else {
                    getSubscribers(selectedSurvey!.id)
                        .then((data) => setSubscribers(data))
                        .catch((err) => {
                            if (err.response) {
                                dispatch(
                                    setErrorMessage({
                                        message: 'Не удалось получить подписчиков этого опроса',
                                    }),
                                );
                            }
                        });
                }
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось добавить пользователя',
                        }),
                    );
                }
            });
    };

    const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === 'Enter') {
            addSubscriberHandle();
            setSubscribersInput('');
        }
    };

    const deleteSubscribeHandler = (email: string) => {
        deleteSubscriber(selectedSurvey!.id, email)
            .then(() => {
                setSubscribers((prev) => prev.filter((sub) => sub.email !== email));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(
                        setErrorMessage({
                            message: 'Не удалось удалить подписчиков этого опроса',
                        }),
                    );
                }
            });
    };

    return (
        <div className={style.content}>
            <FormLabel>Управление участниками</FormLabel>
            <Input
                value={subscribersInput}
                onChange={(e) => setSubscribersInput(e)}
                placeholder='Email'
                type='email'
                onKeyDown={handleKeyDown}
                elevatePlaceholder
            />
            <div className={style.list}>
                {subscribers.map((sub) => (
                    <div key={sub.id} className={style.tag}>
                        <p>
                            {sub.login}: {sub.email}
                        </p>
                        <img
                            className={style.img}
                            src='/X.svg'
                            alt='x'
                            onClick={() => deleteSubscribeHandler(sub.email)}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
}
