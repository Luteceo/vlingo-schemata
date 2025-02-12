import Vue from 'vue'
import Vuex from 'vuex'
import { getField, updateField } from 'vuex-map-fields';

Vue.use(Vuex)

export default new Vuex.Store({
  strict: true,
  state: {
    error: undefined,
    notification: undefined,
    schemaVersion: undefined,
    selected: undefined,
    organization: undefined,
    unit: undefined,
    context: undefined,
    category: undefined,
    schema: undefined,
    version: undefined
  },
  mutations: {
    updateField,
    raiseError (state, error) {
      state.error = error
    },

    dismissError (state) {
      state.error = undefined
    },

    raiseNotification (state, notification) {
      notification.type = notification.type || 'info'
      state.notification = notification
    },

    dismissNotification (state) {
      state.notification = undefined
    },

    selectSchema (state, selected) {
      state.schema = selected
    },
    selectOrganization (state, selected) {
      state.organization = selected
    },

  },
  actions: {

  },
  getters: {
    getField,
    organizationId: state => state.selected?.organizationId ?? undefined,
    unitId: state => state.selected?.unitId ?? undefined,
    contextId: state => state.selected?.contextId ?? undefined,
    schemaId: state => state.selected?.schemaId ?? undefined,
  }
})
