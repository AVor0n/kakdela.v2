import { Text, Title } from '@hh.ru/magritte-ui';
import style from './Info.module.css';
interface InfoItem {
    title: string;
    description: string;
    imageSrc: string;
    serialNumber: number;
}

interface Props {
    item: InfoItem;
}

export function Info({ item }: Props) {
    return (
        <div className={item.serialNumber % 2 === 0 ? style.left : style.right}>
            <img src={item.imageSrc} alt={item.title} className={style.img} />
            <div className={style.content}>
                <Title Element='h3' size='large'>
                    {item.title}
                </Title>
                <Text Element='p' style='secondary' typography='paragraph-1-regular'>
                    {item.description}
                </Text>
            </div>
        </div>
    );
}
