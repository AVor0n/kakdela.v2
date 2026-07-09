import { useNavigate } from 'react-router-dom';
import { Button, Text, Title } from '@hh.ru/magritte-ui';
import { routes } from '@/app/routes';
import style from './NotFound.module.css';

export function NotFound() {
    const navigate = useNavigate();

    return (
        <main className={style.main}>
            <div className={style.blobOne} />
            <div className={style.blobTwo} />

            <div className={style.card}>
                <div className={style.accent} />
                <div className={style.body}>
                    <span className={style.code}>404</span>
                    <Title Element='h2' size='large' alignment='center'>
                        Страница не найдена
                    </Title>
                    <Text Element='p' style='secondary' typography='paragraph-1-regular' className={style.description}>
                        Похоже, такой страницы не существует или она была перемещена. Проверьте адрес или вернитесь на
                        главную.
                    </Text>
                    <div className={style.actions}>
                        <Button mode='primary' style='accent' size='large' onClick={() => navigate(routes.root())}>
                            На главную
                        </Button>
                        <Button mode='secondary' style='neutral' size='large' onClick={() => navigate(routes.survey())}>
                            Мои опросы
                        </Button>
                    </div>
                </div>
            </div>
        </main>
    );
}
