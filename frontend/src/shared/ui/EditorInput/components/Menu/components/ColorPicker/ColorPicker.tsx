// components/ColorPicker/ColorPicker.tsx
import classNames from 'classnames';
import { type ReactNode, useState } from 'react';
import style from './ColorPicker.module.css';

interface ColorOption {
    value: string;
    className: string; // css-класс для фона/цвета кружка
    isActive: boolean;
    letter?: string;
}

interface Props {
    buttonLabel: ReactNode; // 'M' или 'A'
    colors: ColorOption[];
    onSet: (_color: string) => void;
    onReset: () => void;
    isResetDisabled?: boolean;
    children?: ReactNode; // доп. контент в попапе, напр. <input type="color">
}

export function ColorPicker({ buttonLabel, colors, onSet, onReset, isResetDisabled, children }: Props) {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <div className={style.container}>
            <button className={style.button} onClick={() => setIsOpen((v) => !v)}>
                {buttonLabel}
            </button>
            {isOpen && (
                <div className={style.colorEditor}>
                    {children}
                    <div className={style.colors}>
                        {colors.map(({ value, className, isActive, letter }) => (
                            <button
                                key={value}
                                onClick={() => onSet(value)}
                                className={classNames(isActive ? 'is-active' : '', style.button)}
                            >
                                <div className={classNames(style.color, className)}>{letter ?? ''}</div>
                            </button>
                        ))}
                        <button
                            onClick={onReset}
                            disabled={isResetDisabled}
                            className={classNames(style.unsetContainer, style.button)}
                        >
                            <div className={classNames(style.color, style.unset)} />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
