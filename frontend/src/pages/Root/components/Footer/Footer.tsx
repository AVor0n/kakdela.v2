import { Link } from 'react-router-dom';
import { Text } from '@hh.ru/magritte-ui';
import { routes } from '@/app/routes';
import { ProductLogo } from '@/shared/ui/ProductLogo/ProductLogo';
import style from './Footer.module.css';

export function Footer() {
    return (
        <footer className={style.footer}>
            <div className={style.inner}>
                <div className={style.brand}>
                    <ProductLogo to={routes.root()} />
                    <div className={style.tagline}>
                        <Text Element='p' style='secondary' typography='paragraph-2-regular'>
                            Сервис для создания опросов, сбора обратной связи и анализа ответов.
                        </Text>
                    </div>
                </div>
                <nav className={style.nav} aria-label='Навигация по сайту'>
                    <span className={style.navTitle}>Продукт</span>
                    <Link className={style.link} to={routes.survey()}>
                        Опросы
                    </Link>
                    <Link className={style.link} to={routes.login()}>
                        Войти
                    </Link>
                    <Link className={style.link} to={routes.register()}>
                        Регистрация
                    </Link>
                    <Link className={style.link} to='https://mattermost.pyn.ru/school-2026/channels/kakdela-v2-public'>
                        Mattermost
                    </Link>
                    <Link className={style.link} to='https://hh.ru/services'>
                        Все сервисы
                    </Link>
                </nav>
            </div>
            <div className={style.bottom}>
                <Text Element='span' style='tertiary' typography='paragraph-3-regular'>
                    © {new Date().getFullYear()} ООО "Хэдхантер" - Создано в Школе программистов hh.ru
                </Text>
            </div>
        </footer>
    );
}
