import classNames from 'classnames';
import type { ChangeEvent } from 'react';
import style from './ImageAttachmentControl.module.css';

type Props = {
    inputId: string;
    hasImage: boolean;
    disabled?: boolean;
    onChange: (_event: ChangeEvent<HTMLInputElement>) => void;
    onDelete: () => void;
};

export function ImageAttachmentControl({ inputId, hasImage, disabled = false, onChange, onDelete }: Props) {
    return (
        <div className={style.container}>
            <input
                className={style.hiddenInput}
                id={inputId}
                type='file'
                accept='image/*'
                disabled={disabled}
                onChange={onChange}
            />
            <label
                className={classNames(style.button, { [style.disabled]: disabled })}
                htmlFor={inputId}
                title={hasImage ? 'Заменить изображение' : 'Добавить изображение'}
                aria-label={hasImage ? 'Заменить изображение' : 'Добавить изображение'}
            >
                <img src='/img.svg' alt='' />
            </label>
            {hasImage && (
                <button
                    className={style.button}
                    type='button'
                    disabled={disabled}
                    title='Удалить изображение'
                    aria-label='Удалить изображение'
                    onClick={onDelete}
                >
                    <img src='/trash.svg' alt='' />
                </button>
            )}
        </div>
    );
}
