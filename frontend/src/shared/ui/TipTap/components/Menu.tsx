import { Editor, useEditorState } from '@tiptap/react';
import style from './Menu.module.css';
import { useState } from 'react';
import classNames from 'classnames';

interface Props {
    editor: Editor;
}
export function Menu({ editor }: Props) {
    const [colorPopover, setColorPopover] = useState<boolean>(false);
    const { isCode } = useEditorState({
        editor,
        selector: (ctx) => ({
            isCode: ctx.editor.isActive('code') ?? false,
        }),
    });

    const editorState = useEditorState({
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
            <button className={classNames(style.bold, style.button)} onClick={() => editor.commands.toggleBold()}>
                B
            </button>
            <button className={classNames(style.italic, style.button)} onClick={() => editor.commands.toggleItalic()}>
                𝐈
            </button>
            <button
                className={classNames(style.underline, style.button)}
                onClick={() => editor.commands.toggleUnderline()}
            >
                U
            </button>
            <button className={style.button} onClick={() => editor.commands.toggleStrike()}>
                S
            </button>
            <button
                onClick={() => editor.chain().focus().toggleCode().run()}
                className={classNames(isCode ? 'is-active' : '', style.button)}
            >
                {'</>'}
            </button>
            <button className={style.button} onClick={() => setColorPopover(!colorPopover)}>
                M
            </button>
            {colorPopover && (
                <div className={style.colorEditor}>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight().run()}
                        className={classNames(editorState.isHighlight ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.highlight)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#ffc078' }).run()}
                        className={classNames(editorState.isOrange ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.orange)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#8ce99a' }).run()}
                        className={classNames(editorState.isGreen ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.green)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#74c0fc' }).run()}
                        className={classNames(editorState.isBlue ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.blue)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#b197fc' }).run()}
                        className={classNames(editorState.isPurple ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.purple)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: 'red' }).run()}
                        className={classNames(editorState.isRed ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.red)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().toggleHighlight({ color: '#ffa8a8' }).run()}
                        className={classNames(editorState.isLightRed ? 'is-active' : '', style.button)}
                    >
                        <div className={classNames(style.color, style.rose)} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().unsetHighlight().run()}
                        disabled={!editorState.isHighlight}
                        className={classNames(style.unsetContainer, style.button)}
                    >
                        <div className={classNames(style.color, style.unset)} />
                    </button>
                </div>
            )}
        </div>
    );
}
