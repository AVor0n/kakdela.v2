import { useNavigate } from 'react-router-dom';
import { Button, Text, Title } from '@hh.ru/magritte-ui';
import { routes } from '@/app/routes';
import style from './Cta.module.css';

interface Props {
    onCreateClick: () => void;
}

export function Cta({ onCreateClick }: Props) {
    const navigate = useNavigate();

    return (
        <section className={style.section}>
            <div className={style.card}>
                <Title Element='h2' size='large' alignment='center' style='constant'>
                    Готовы собрать первые ответы?
                </Title>
                <div className={style.description}>
                    <Text Element='p' style='constant' typography='paragraph-1-regular'>
                        Создайте опрос за пару минут и начните получать ответы уже сегодня — это бесплатно.
                    </Text>
                </div>
                <div className={style.actions}>
                    <Button mode='primary' style='constant' size='large' onClick={onCreateClick}>
                        Создать опрос
                    </Button>
                    <Button mode='secondary' style='constant' size='large' onClick={() => navigate(routes.survey())}>
                        Мои опросы
                    </Button>
                </div>
            </div>
        </section>
    );
}
