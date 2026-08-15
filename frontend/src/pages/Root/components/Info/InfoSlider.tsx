import { useCallback, useRef, useState } from 'react';
import type { KeyboardEvent as ReactKeyboardEvent, PointerEvent as ReactPointerEvent } from 'react';
import { Info } from './Info';
import style from './InfoSlider.module.css';
import { ArrowLeftOutlinedSize16, ArrowRightOutlinedSize16 } from '@hh.ru/magritte-ui/icon';

interface InfoItem {
    title: string;
    description: string;
    imageSrc: string;
    serialNumber: number;
}

interface Props {
    items: InfoItem[];
}

const SWIPE_THRESHOLD = 50;

export function InfoSlider({ items }: Props) {
    const [activeIndex, setActiveIndex] = useState(0);
    const dragStartX = useRef<number | null>(null);

    const goTo = useCallback(
        (index: number) => {
            setActiveIndex(((index % items.length) + items.length) % items.length);
        },
        [items.length],
    );

    const handlePrev = () => goTo(activeIndex - 1);
    const handleNext = () => goTo(activeIndex + 1);

    const handleKeyDown = (event: ReactKeyboardEvent<HTMLDivElement>) => {
        if (event.key === 'ArrowLeft') {
            handlePrev();
        } else if (event.key === 'ArrowRight') {
            handleNext();
        }
    };

    const handlePointerDown = (event: ReactPointerEvent<HTMLDivElement>) => {
        dragStartX.current = event.clientX;
    };

    const handlePointerUp = (event: ReactPointerEvent<HTMLDivElement>) => {
        if (dragStartX.current === null) {
            return;
        }
        const diff = event.clientX - dragStartX.current;
        dragStartX.current = null;

        if (diff > SWIPE_THRESHOLD) {
            handlePrev();
        } else if (diff < -SWIPE_THRESHOLD) {
            handleNext();
        }
    };

    return (
        <div
            className={style.slider}
            role='region'
            aria-roledescription='slider'
            aria-label='Возможности сервиса'
            tabIndex={0}
            onKeyDown={handleKeyDown}
        >
            <div
                className={style.viewport}
                onPointerDown={handlePointerDown}
                onPointerUp={handlePointerUp}
                onPointerLeave={() => {
                    dragStartX.current = null;
                }}
            >
                <div className={style.track} style={{ transform: `translateX(-${activeIndex * 100}%)` }}>
                    {items.map((item) => (
                        <div
                            className={style.slide}
                            key={item.serialNumber}
                            aria-hidden={item.serialNumber - 1 !== activeIndex}
                        >
                            <Info item={item} />
                        </div>
                    ))}
                </div>
            </div>

            <div className={style.controls}>
                <button type='button' className={style.navButton} onClick={handlePrev} aria-label='Предыдущий слайд'>
                    <ArrowLeftOutlinedSize16 />
                </button>

                <div className={style.dots}>
                    {items.map((item, index) => (
                        <button
                            key={item.serialNumber}
                            type='button'
                            className={index === activeIndex ? `${style.dot} ${style.dotActive}` : style.dot}
                            aria-label={`Перейти к слайду ${index + 1}`}
                            aria-current={index === activeIndex}
                            onClick={() => goTo(index)}
                        />
                    ))}
                </div>

                <button type='button' className={style.navButton} onClick={handleNext} aria-label='Следующий слайд'>
                    <ArrowRightOutlinedSize16 />
                </button>
            </div>
        </div>
    );
}
