import { addSubscriber, deleteSubscriber, getSubscribers } from '@/api/subscribers';
import { useAppSelector } from '@/hooks/useAppSelector';
import type { Subscribers } from '@/shared/types/Subscribers.type';
import { FormLabel, Input } from '@hh.ru/magritte-ui';
import { useEffect, useState, type KeyboardEvent } from 'react';
import style from './SubscribersInput.module.css';
import { ErrorBlock } from '../../ErrorBlock/ErrorBlock';
import type { Error } from '@/shared/types/Error.type';
export function SubscribersInput() {
    const { selectedSurvey } = useAppSelector((state) => state.survey);
    const [subscribers, setSubscribers] = useState<Subscribers[]>([]);
    const [subscribersInput, setSubscribersInput] = useState<string>('');
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        getSubscribers(selectedSurvey!.id).then((data) => setSubscribers(data));
    }, []);

    const addSubscriberHandle = () => {
        addSubscriber(selectedSurvey!.id, subscribersInput)
            .then((data) => {
                if (data.alreadySubscribedEmails.length > 0) {
                    setError({ message: 'Этот пользователь уже добавлен' });
                } else if (data.notFoundEmails.length > 0) {
                    setError({ message: 'Пользователя с таким email не существует' });
                } else {
                    getSubscribers(selectedSurvey!.id)
                        .then((data) => setSubscribers(data))
                        .catch((err) => {
                            if (err.response) {
                                setError(err.response.data);
                            }
                        });
                }
            })
            .catch((err) => {
                if (err.response) {
                    setError(err.response.data);
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
                    setError({ message: 'Не удалось удалить обязательного пользователя' });
                }
            });
    };

    return (
        <div className={style.content}>
            {error && <ErrorBlock error={error} setError={setError} />}
            <FormLabel>Пользователи для обязательного прохождения</FormLabel>
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
