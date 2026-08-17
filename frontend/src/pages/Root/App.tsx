import { useNavigate } from 'react-router-dom';
import { Button } from '@hh.ru/magritte-ui';
import { routes } from '@/app/routes';
import style from './App.module.css';
import { createSurvey } from '@/api/survey';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { useAppSelector } from '@/hooks/useAppSelector';
import { AccountDetail } from '@/shared/ui/AccountDetail/AccountDetail';
import { ProductLogo } from '@/shared/ui/ProductLogo/ProductLogo';
import { InfoSlider } from './components/Info/InfoSlider';
import { HowItWorks } from './components/HowItWorks/HowItWorks';
import { UseCases } from './components/UseCases/UseCases';
import { Cta } from './components/Cta/Cta';
import { Footer } from './components/Footer/Footer';

interface InfoItem {
    title: string;
    description: string;
    imageSrc: string;
    serialNumber: number;
}

const INFO: InfoItem[] = [
    {
        title: 'Конструктор опросов',
        description:
            'Добавлйяте вопросы, меняйте порядок простым перетаскиванием и настраивайте внешний вид без лишних усилий',
        imageSrc: '/images/info/constructor.png',
        serialNumber: 1,
    },
    {
        title: 'Ветвление и логика переходов',
        description:
            'Показывайте разным респондентам разные вопросы в зависимости от их ответов — стройте гибкие сценарии прохождения опроса.',
        imageSrc: '/images/info/branching.png',
        serialNumber: 2,
    },
    {
        title: 'Роли и права доступа',
        description:
            'Разграничивайте доступ между владельцами, редакторами и аналитиками, чтобы команда работала над опросом безопасно и слаженно.',
        imageSrc: '/images/info/roles.png',
        serialNumber: 3,
    },
    {
        title: 'Библиотека шаблонов',
        description:
            'Используйте шаблоны опросов под разные задачи — от обратной связи по продукту до оценки удовлетворённости сотрудников.',
        imageSrc: '/images/info/templates.png',
        serialNumber: 4,
    },
    {
        title: 'Разные типы вопросов',
        description:
            'Одиночный и множественный выбор, краткий ответ, свободный текст, дата и другие форматы — выбирайте подходящий под каждую задачу.',
        imageSrc: '/images/info/question-types.png',
        serialNumber: 5,
    },
    {
        title: 'Аналитика в реальном времени',
        description: 'Следите за ответами по мере их поступления и выгружайте результаты для дальнейшего анализа.',
        imageSrc: '/images/info/analytics.png',
        serialNumber: 6,
    },
    {
        title: 'Публикация и распространение',
        description:
            'Публикуйте опрос по ссылке или встраивайте на сайт — собирайте ответы там, где удобно вашей аудитории.',
        imageSrc: '/images/info/publishing.png',
        serialNumber: 7,
    },
    {
        title: 'Уведомления',
        description:
            'Отправляйте пользователям уведомления и настраивайте их как обязательные для прохождения, а также подключайте периодические напоминания о новых опросах.',
        imageSrc: '/images/info/notifications.png',
        serialNumber: 8,
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
                    <h1 className={style.title}>
                        <ProductLogo to={routes.root()} />
                    </h1>
                </section>
                <section className={style.auth}>
                    {account !== null ? (
                        <AccountDetail />
                    ) : (
                        <>
                            <Button
                                className={style.registrationButton}
                                mode='secondary'
                                style='accent'
                                onClick={() => navigate(routes.register())}
                            >
                                <span className={style.registrationButtonText}>Регистрация</span>
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
                <InfoSlider items={INFO} />
                <HowItWorks />
                <UseCases />
                <Cta onCreateClick={handleCreateClick} />
            </main>
            <Footer />
        </>
    );
}

export default App;
