import type { Template, TemplateListItem } from '@/shared/types/Survey.type';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface ITemplateState {
    templates: TemplateListItem[];
    selectedTemplate: Template | null;
}

const initialState: ITemplateState = {
    templates: [],
    selectedTemplate: null,
};

export const templateSlice = createSlice({
    name: 'template',
    initialState,
    reducers: {
        setTemplates: (state, action: PayloadAction<TemplateListItem[]>) => {
            state.templates = action.payload;
        },
        addTemplate: (state, action: PayloadAction<TemplateListItem>) => {
            state.templates.push(action.payload);
        },
        removeTemplate: (state, action: PayloadAction<{ templateId: string }>) => {
            const { templateId } = action.payload;
            state.templates = state.templates.filter((template) => template.id !== templateId);
        },
        setSelectedTemplate: (state, action: PayloadAction<{ template: Template | null }>) => {
            const { template } = action.payload;
            state.selectedTemplate = template;
        },
    },
});

export const { setTemplates, setSelectedTemplate, removeTemplate, addTemplate } = templateSlice.actions;
export default templateSlice.reducer;
