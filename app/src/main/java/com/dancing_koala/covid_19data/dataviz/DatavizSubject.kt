package com.dancing_koala.covid_19data.dataviz

import com.dancing_koala.covid_19data.core.Color
import com.dancing_koala.covid_19data.data.DataCategory
import com.dancing_koala.covid_19data.data.TimeLineDataSet


data class DatavizSubject(val timeLineDataSet: TimeLineDataSet, val associatedColor: Color)

data class DataVizCategoryWithSubjects(val dataCategory: DataCategory, val subjects: List<DatavizSubject>)
