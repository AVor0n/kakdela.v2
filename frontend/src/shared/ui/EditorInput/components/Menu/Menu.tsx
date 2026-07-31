import { Editor, useEditorState } from '@tiptap/react';
import style from './Menu.module.css';
import classNames from 'classnames';
import { HeadingSelect } from './components/HeadingSelect/HeadingSelect';
import { TextColor } from './components/TextColor/TextColor';
import { MarkColor } from './components/MarkColor/MarkColor';

interface Props {
    editor: Editor;
    isHeading?: boolean;
    isTextColor?: boolean;
    isMarkColor?: boolean;
}
export function Menu({ editor, isHeading = false, isTextColor = false, isMarkColor = false }: Props) {
    const { isCode } = useEditorState({
        editor,
        selector: (ctx) => ({
            isCode: ctx.editor.isActive('code') ?? false,
        }),
    });

    return (
        <div className={style.content}>
            {isHeading && <HeadingSelect editor={editor} />}
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
            {isMarkColor && <MarkColor editor={editor} />}
            {isTextColor && <TextColor editor={editor} />}
        </div>
    );
}
