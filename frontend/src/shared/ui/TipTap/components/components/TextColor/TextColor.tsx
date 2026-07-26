import { Editor, useEditorState } from '@tiptap/react';
import style from './TextColor.module.css';
import classNames from 'classnames';
import { useState } from 'react';
interface Props {
    editor: Editor;
}

export function TextColor({ editor }: Props) {
    const [textColorPopover, setTextColorPopover] = useState<boolean>(false);

    const textColorState = useEditorState({
        editor,
        selector: (ctx) => {
            return {
                color: ctx.editor.getAttributes('textStyle').color,
                isPurple: ctx.editor.isActive('textStyle', { color: '#958DF1' }),
                isRed: ctx.editor.isActive('textStyle', { color: '#F98181' }),
                isOrange: ctx.editor.isActive('textStyle', { color: '#FBBC88' }),
                isBlack: ctx.editor.isActive('textStyle', { color: '#000' }),
                isBlue: ctx.editor.isActive('textStyle', { color: '#70CFF8' }),
                isTeal: ctx.editor.isActive('textStyle', { color: '#94FADB' }),
                isGreen: ctx.editor.isActive('textStyle', { color: '#B9F18D' }),
            };
        },
    });
    return (
        <div className={style.container}>
            <button
                className={style.button}
                onClick={() => {
                    setTextColorPopover(!textColorPopover);
                }}
            >
                A
            </button>
            {textColorPopover && (
                <div className={style.textColorEditor}>
                    <input
                        type='color'
                        onInput={(event) => editor.chain().focus().setColor(event.currentTarget.value).run()}
                        value={textColorState.color ?? '#000000'}
                        data-testid='setColor'
                    />
                    <div className={style.textColors}>
                        <button
                            onClick={() => editor.chain().focus().setColor('#958DF1').run()}
                            className={classNames(textColorState.isPurple ? 'is-active' : '', style.button)}
                            data-testid='setPurple'
                        >
                            <div className={classNames(style.color, style.textPurple)}>A</div>
                        </button>
                        <button
                            onClick={() => editor.chain().focus().setColor('#F98181').run()}
                            className={classNames(textColorState.isRed ? 'is-active' : '', style.button)}
                            data-testid='setRed'
                        >
                            <div className={classNames(style.color, style.textRed)}>A</div>
                        </button>
                        <button
                            onClick={() => editor.chain().focus().setColor('#FBBC88').run()}
                            className={classNames(textColorState.isOrange ? 'is-active' : '', style.button)}
                            data-testid='setOrange'
                        >
                            <div className={classNames(style.color, style.textOrange)}>A</div>
                        </button>
                        <button
                            onClick={() => editor.chain().focus().setColor('#000000').run()}
                            className={classNames(textColorState.isBlack ? 'is-active' : '', style.button)}
                            data-testid='setBlack'
                        >
                            <div className={classNames(style.color)}>A</div>
                        </button>
                        <button
                            onClick={() => editor.chain().focus().setColor('#70CFF8').run()}
                            className={classNames(textColorState.isBlue ? 'is-active' : '', style.button)}
                            data-testid='setBlue'
                        >
                            <div className={classNames(style.color, style.textBlue)}>A</div>
                        </button>
                        <button
                            onClick={() => editor.chain().focus().setColor('#94FADB').run()}
                            className={classNames(textColorState.isTeal ? 'is-active' : '', style.button)}
                            data-testid='setTeal'
                        >
                            <div className={classNames(style.color, style.textTeal)}>A</div>
                        </button>
                        <button
                            onClick={() => editor.chain().focus().setColor('#B9F18D').run()}
                            className={classNames(textColorState.isGreen ? 'is-active' : '', style.button)}
                            data-testid='setGreen'
                        >
                            <div className={classNames(style.color, style.textGreen)}>A</div>
                        </button>
                        <button
                            onClick={() => editor.chain().focus().unsetColor().run()}
                            className={classNames(style.unsetContainer, style.button)}
                            data-testid='unsetColor'
                        >
                            <div className={classNames(style.color, style.unset)} />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
