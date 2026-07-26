import { Editor, useEditorState } from '@tiptap/react';
import classNames from 'classnames';
import { useState } from 'react';
import style from './MarkColor.module.css';
interface Props {
    editor: Editor;
}

export function MarkColor({ editor }: Props) {
    const [colorPopover, setColorPopover] = useState<boolean>(false);

    const textMarkState = useEditorState({
        editor,
        selector: (ctx) => ({
            isHighlight: ctx.editor.isActive('highlight') ?? false,
            isOrange: ctx.editor.isActive('highlight', { color: '#ffc078' }) ?? false,
            isGreen: ctx.editor.isActive('highlight', { color: '#8ce99a' }) ?? false,
            isBlue: ctx.editor.isActive('highlight', { color: '#74c0fc' }) ?? false,
            isPurple: ctx.editor.isActive('highlight', { color: '#b197fc' }) ?? false,
            isRed: ctx.editor.isActive('highlight', { color: 'red' }) ?? false,
            isLightRed: ctx.editor.isActive('highlight', { color: '#ffa8a8' }) ?? false,
        }),
    });

    return (
        <div className={style.content}>
            <button
                className={style.button}
                onClick={() => {
                    setColorPopover(!colorPopover);
                }}
            >
                M
            </button>
            {colorPopover && (
                <div className={style.colorEditor}>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#ffc078' }).run()}
                        className={classNames(textMarkState.isOrange ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.orange)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#8ce99a' }).run()}
                        className={classNames(textMarkState.isGreen ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.green)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#74c0fc' }).run()}
                        className={classNames(textMarkState.isBlue ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.blue)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#b197fc' }).run()}
                        className={classNames(textMarkState.isPurple ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.purple)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: 'red' }).run()}
                        className={classNames(textMarkState.isRed ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.red)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#ffa8a8' }).run()}
                        className={classNames(textMarkState.isLightRed ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.rose)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().unsetHighlight().run()}
                        disabled={!textMarkState.isHighlight}
                        className={classNames(style.unsetContainer, style.button)}
                    >
                        <div className={classNames(style.color, style.unset)} />
                    </button>
                </div>
            )}
        </div>
    );
}
