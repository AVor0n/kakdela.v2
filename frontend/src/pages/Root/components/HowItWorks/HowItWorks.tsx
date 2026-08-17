import type { ComponentType } from 'react';
import { Text, Title } from '@hh.ru/magritte-ui';
import {
    ChartBarOutlinedSize24,
    GearOutlinedSize24,
    PenHyperstarOutlinedSize24,
    ShareWebOutlinedSize24,
} from '@hh.ru/magritte-ui/icon';
import style from './HowItWorks.module.css';

interface Step {
    number: string;
    title: string;
    description: string;
    Icon: ComponentType;
}

const STEPS: Step[] = [
    {
        number: '01',
        title: 'Создайте опрос',
        description: 'Начните с чистого листа или используйте шаблон, которым поделился коллега.',
        Icon: PenHyperstarOutlinedSize24,
    },
    {
        number: '02',
        title: 'Настройте вопросы',
        description: 'Добавьте нужные типы вопросов и постройте логику переходов между ними.',
        Icon: GearOutlinedSize24,
    },
    {
        number: '03',
        title: 'Поделитесь ссылкой',
        description: 'Опубликуйте опрос и разошлите ссылку респондентам любым удобным способом.',
        Icon: ShareWebOutlinedSize24,
    },
    {
        number: '04',
        title: 'Анализируйте ответы',
        description: 'Следите за статистикой в реальном времени и выгружайте результаты одним кликом.',
        Icon: ChartBarOutlinedSize24,
    },
];

export function HowItWorks() {
    return (
        <section className={style.section}>
            <Title Element='h2' size='large' alignment='center'>
                Как это работает
            </Title>
            <div className={style.steps}>
                {STEPS.map((step) => (
                    <div className={style.step} key={step.number}>
                        <div className={style.iconWrap}>
                            <step.Icon />
                        </div>
                        <span className={style.number}>{step.number}</span>
                        <Title Element='h3' size='small' alignment='center'>
                            {step.title}
                        </Title>
                        <Text Element='p' style='secondary' typography='paragraph-2-regular'>
                            {step.description}
                        </Text>
                    </div>
                ))}
            </div>
        </section>
    );
}
