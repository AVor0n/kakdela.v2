import type { ComponentType } from 'react';
import { Text, Title } from '@hh.ru/magritte-ui';
import {
    BriefcaseOutlinedSize24,
    GraduationHatOutlinedSize24,
    LightbulbOnOutlinedSize24,
    RocketOutlinedSize24,
} from '@hh.ru/magritte-ui/icon';
import style from './UseCases.module.css';

interface UseCase {
    title: string;
    description: string;
    Icon: ComponentType;
}

const USE_CASES: UseCase[] = [
    {
        title: 'HR и рекрутмент',
        description: 'Оценивайте вовлечённость сотрудников, собирайте обратную связь после собеседований и адаптации.',
        Icon: BriefcaseOutlinedSize24,
    },
    {
        title: 'Образование',
        description: 'Проверяйте знания студентов, собирайте отзывы о курсах и преподавателях.',
        Icon: GraduationHatOutlinedSize24,
    },
    {
        title: 'Продуктовые команды',
        description: 'Изучайте мнение пользователей о новых фичах и приоритизируйте бэклог на основе данных.',
        Icon: LightbulbOnOutlinedSize24,
    },
    {
        title: 'Ивенты и сообщества',
        description: 'Собирайте регистрации, узнавайте настроение участников и улучшайте будущие мероприятия.',
        Icon: RocketOutlinedSize24,
    },
];

export function UseCases() {
    return (
        <section className={style.section}>
            <Title Element='h2' size='large' alignment='center'>
                Для кого подойдёт KakDela
            </Title>
            <div className={style.grid}>
                {USE_CASES.map((useCase) => (
                    <div className={style.card} key={useCase.title}>
                        <div className={style.iconWrap}>
                            <useCase.Icon />
                        </div>
                        <Title Element='h3' size='small'>
                            {useCase.title}
                        </Title>
                        <Text Element='p' style='secondary' typography='paragraph-2-regular'>
                            {useCase.description}
                        </Text>
                    </div>
                ))}
            </div>
        </section>
    );
}
