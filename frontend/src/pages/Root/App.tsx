import { useNavigate } from 'react-router-dom';
import { Button, Card, Text, Title } from '@hh.ru/magritte-ui';
import { routes } from '@/app/routes';
import style from './App.module.css';
import { createSurvey } from '@/api/survey';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { useAppSelector } from '@/hooks/useAppSelector';
import { AccountDetail } from '@/shared/ui/AccountDetail/AccountDetail';

const FEATURES = [
    {
        icon: '⚡',
        title: 'Быстрое создание',
        description: 'Соберите опрос за пару минут с помощью гибкого конструктора вопросов.',
    },
    {
        icon: '📊',
        title: 'Наглядная аналитика',
        description: 'Следите за ответами в реальном времени и делайте выводы без лишних таблиц.',
    },
    {
        icon: '🤝',
        title: 'Для команд',
        description: 'Делитесь опросами с коллегами и собирайте обратную связь сообща.',
    },
];

function App() {
    const navigate = useNavigate();
    const { account } = useAppSelector((state) => state.account);
    const dispatch = useAppDispatch();
    const handleCreateClick = () => {
        createSurvey()
            .then((data) => {
                navigate(routes.surveyQuestions(data.id));
            })
            .catch((err) => {
                if (err.response) {
                    dispatch(setErrorMessage({ message: 'Не удалось создать опрос' }));
                }
            });
    };

    return (
        <>
            <header className={style.header}>
                <section className={style.logo}>
                    <img src='/hh-logo.svg' alt='HH' width={50} />
                    <h1 className={style.title}>KakDela V2.0</h1>
                </section>
                <section className={style.auth}>
                    {account !== null ? (
                        <AccountDetail />
                    ) : (
                        <>
                            <Button mode='secondary' style='accent' onClick={() => navigate(routes.register())}>
                                Регистрация
                            </Button>
                            <Button mode='primary' style='accent' onClick={() => navigate(routes.login())}>
                                Войти
                            </Button>
                        </>
                    )}
                </section>
            </header>
            <main className={style.main}>
                <div className={style.blobOne} />
                <div className={style.blobTwo} />

                <section className={style.hero}>
                    <div className={style.heroCard}>
                        <div className={style.heroAccent} />
                        <div className={style.heroBody}>
                            <Title Element='h2' size='extra-large' alignment='center'>
                                Опросы, которые приятно проходить
                            </Title>
                            <Text
                                Element='p'
                                style='secondary'
                                typography='paragraph-1-regular'
                                className={style.heroDescription}
                            >
                                KakDela помогает создавать красивые опросы, собирать ответы и превращать их в понятные
                                выводы — без лишних усилий.
                            </Text>
                            <div className={style.heroActions}>
                                <Button mode='primary' style='accent' size='large' onClick={handleCreateClick}>
                                    Создать опрос
                                </Button>
                                <Button
                                    mode='secondary'
                                    style='neutral'
                                    size='large'
                                    onClick={() => navigate(routes.survey())}
                                >
                                    Смотреть опросы
                                </Button>
                            </div>
                        </div>
                    </div>
                </section>

                <section className={style.features}>
                    {FEATURES.map((feature) => (
                        <Card key={feature.title} padding={24} borderRadius={24} className={style.featureCard}>
                            <Text Element='h3' typography='subtitle-1-semibold'>
                                <span className={style.featureIcon}>{feature.icon} </span>
                                {feature.title}
                            </Text>
                            <Text Element='p' style='secondary' typography='paragraph-3-regular'>
                                {feature.description}
                            </Text>
                        </Card>
                    ))}
                </section>
            </main>
        </>
    );
}

export default App;
